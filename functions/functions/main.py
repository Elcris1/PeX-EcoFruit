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
    firebase_admin.initialize_app()
 
db = firestore.client()
logger = logging.getLogger(__name__)

# utils
def _get_active_tokens_for_user(user_id: str) -> list[str]:
    """Devuelve todos los FCM tokens activos de un usuario."""
    document_ref = db.collection("users").document(user_id)
    if not document_ref.get().exists:
        logger.warning(f"User {user_id} does not exist in Firestore.")
        return []
    
    
    tokens_ref = (
        document_ref
        .collection("fcm_tokens")
        .where("active", "==", True)
        .stream()
    )
    return [doc.to_dict().get("token") for doc in tokens_ref if doc.to_dict().get("token")]

def _send_multicast(tokens: list[str], title: str, body: str, data: dict[str, Any]) -> None:
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
 
    response = messaging.send_each_for_multicast(message)
    logger.info(
        "FCM multicast: %d éxito, %d fallo",
        response.success_count,
        response.failure_count,
    )
    return response

# Listenners
@firestore_fn.on_document_updated(
    document="conversations/{conversation_id}/messages/{message_id}"
)
def on_conversation_updated(event: firestore_fn.Event[firestore_fn.Change[firestore.DocumentSnapshot]]) -> None:
    message: dict[str, Any] = event.data.to_dict()
    sender_id: str = message.get("senderId", "")
    text: str = message.get("text", "Te ha enviado un mensaje.")
    conversation_id: str = message.get("conversation_id", "")

    if not sender_id:
        logger.warning("Message does not have a senderId.")
        return
    
    converation_doc = db.collections("conversations").document(conversation_id).get()
    if not converation_doc.exists:
        logger.warning(f"Conversation {conversation_id} does not exist.")
        return
    
    conversation: dict[str, Any] = converation_doc.to_dict()
    participants: list[str] = conversation.get("participantsId", [])
    receiverId = next((uid for uid in participants if uid != sender_id), None)

    if not receiverId:
        logger.warning(f"No receiver found in conversation {conversation_id} for sender {sender_id}.")
        return
    
    tokens = _get_active_tokens_for_user(receiverId)
    if not tokens:
        logger.info(f"No active FCM tokens found for user {receiverId}.")
        return
    
    sender_doc = db.collection("users").document(sender_id).get()
    sender_name: str = sender_doc.to_dict().get("name", "UserName")
    
    _send_multicast(
        tokens=tokens,
        title=f"💬 {sender_name}",
        body=text[:100], 
        data={
            "type": "new_message",
            "chat_id": event.params["chatId"],
            "screen": "chat",
        },
    )



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