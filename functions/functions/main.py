# Welcome to Cloud Functions for Firebase for Python!
# To get started, simply uncomment the below code or create your own.
# Deploy with `firebase deploy`

import json
import logging
from typing import Any
import firebase_admin
from firebase_functions import https_fn, firestore_fn, options
from firebase_functions.options import set_global_options
from firebase_admin import initialize_app, firestore, credentials, messaging

# For cost control, you can set the maximum number of containers that can be
# running at the same time. This helps mitigate the impact of unexpected
# traffic spikes by instead downgrading performance. This limit is a per-function
# limit. You can override the limit for each function using the max_instances
# parameter in the decorator, e.g. @https_fn.on_request(max_instances=5).
set_global_options(max_instances=10)

# Initialization
if not firebase_admin._apps:
    logging.basicConfig(level=logging.INFO)
    logger = logging.getLogger(__name__)
    firebase_admin.initialize_app()


# utils
def _get_active_tokens_for_user(user_id: str, db: firestore.Client) -> list[str]:
    print(f"Fetching active FCM tokens for user: {user_id}")
    """Devuelve todos los FCM tokens activos de un usuario."""
    document_ref = db.collection("users").document(user_id)
    if not document_ref.get().exists:
        logger.warning(f"User {user_id} does not exist in Firestore.")
        return []
    
    
    tokens_ref = (
        document_ref
        .collection("fcm_token")
        .where("active", "==", True)
        .stream()
    )
    return [doc.to_dict().get("token") for doc in tokens_ref]

def _send_multicast(tokens: list[str], title: str, body: str, data: dict[str, Any]) -> Any:
    if not tokens:
        logger.warning("No hay tokens FCM para enviar notificación.")
        return None
 
    message = messaging.MulticastMessage(
        tokens=tokens,
        notification=messaging.Notification(title=title, body=body),
        data=data or {},
        android=messaging.AndroidConfig(
            priority="high",
            notification=messaging.AndroidNotification(
                channel_id="ecofruit_channel",
                icon="ic_notification",
                color="#4CAF50",  # EcoMoss
            ),
        ),
        apns=messaging.APNSConfig(
            payload=messaging.APNSPayload(
                aps=messaging.Aps(badge=1, sound="default")
            )
        ),
    )
    logger.info("Trying to send FCM multicast message to %d tokens with title '%s'", len(tokens), title)
 
    # Use the correct Firebase Admin SDK function for sending multicast messages
    response = messaging.send_each_for_multicast(message)
    print(f"FCM multicast exito: {response.success_count}, fallo: {response.failure_count}")
    return response

# Listenners
@firestore_fn.on_document_created(
    document="conversations/{conversation_id}/messages/{message_id}"
)
def on_message_created(event: firestore_fn.Event[firestore_fn.DocumentSnapshot | None]) -> None:
    
    # 1. Validar existencia de datos
    if event.data is None:
        logger.warning("No data found in the event.")
        return

    try:
        logger.info(f"Conversation created: %s", event.params)
        print(f"Conversation created: {event.params}, {event.data.to_dict()}")
        db = firestore.client()
        message: dict[str, Any] = event.data.to_dict()
        sender_id: str = message.get("senderId", "")
        text: str = message.get("text", "Te ha enviado un mensaje.")
        conversation_id: str = event.params.get("conversation_id", "")

        db.collection("audit_logs").add({
            "event": "message_created",
            "additional_info": {
                "conversation_id": conversation_id,
                "sender_id": sender_id,
                "text": text,
            },
            "timestamp": firestore.SERVER_TIMESTAMP,
        })

        if not sender_id:
            logger.warning("Message does not have a senderId.")
            return
        
        conversation_doc = db.collection("conversations").document(conversation_id).get()
        if not conversation_doc.exists:
            print(f"Conversation {conversation_id} does not exist.")
            logger.warning(f"Conversation {conversation_id} does not exist.")
            return
        
        conversation: dict[str, Any] = conversation_doc.to_dict() or {}
        participants: list[str] = conversation.get("participantsId", [])
        receiverId = next((uid for uid in participants if uid != sender_id), None)
        print(f"Participants in conversation: {participants}, sender: {sender_id}, receiver: {receiverId}\n{conversation}")

        if not receiverId:
            print(f"No receiver found in conversation {conversation_id} for sender {sender_id}.")
            logger.warning(f"No receiver found in conversation {conversation_id} for sender {sender_id}.")
            return
        
        tokens = _get_active_tokens_for_user(receiverId, db)
        print(f"Active FCM tokens for user {receiverId}: {tokens}")
        if len(tokens) == 0:
            print(f"No active FCM tokens found for user {receiverId}.")
            logger.info(f"No active FCM tokens found for user {receiverId}.")
            return
        
        sender_doc = db.collection("users").document(sender_id).get()
        sender_dict = sender_doc.to_dict() or {}
        sender_name: str = sender_dict.get("name", "UserName")

        print(f"Sending notification to user {receiverId} from {sender_name} with text: {text}")

        _send_multicast(
            tokens=tokens,
            title=f"💬 {sender_name}",
            body=text[:100], 
            data={
                "type": "new_message",
                # event.params uses the path variable name 'conversation_id'
                "chat_id": event.params.get("conversation_id", conversation_id),
                "screen": "chat",
            },
        )
        print(f"Notification sent to user {receiverId} with tokens: {tokens}")
    except Exception as e:
        print(f"Error processing notifications: {str(e)}")
        logger.error(f"Error processing notification: {str(e)}", exc_info=True)



@firestore_fn.on_document_created(
    document="products/{product_id}"
)
def on_product_created(event: firestore_fn.Event[firestore.DocumentSnapshot]) -> None:
    product: dict[str, Any] = event.data.to_dict()
    product_id: str = event.data.id
    name: str = product.get("name", "")
    price: float = product.get("price", 0.0)
    timestamp: int = product.get("timestamp", 0)

# initialize_app()
#
#
# @https_fn.on_request()
# def on_request_example(req: https_fn.Request) -> https_fn.Response:
#     return https_fn.Response("Hello world!")