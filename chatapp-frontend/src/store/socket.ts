import { defineStore } from "pinia";
import {useUserStore} from "@src/store/user";
import {Client, frameCallbackType, IFrame, IMessage as StompMessage} from "@stomp/stompjs";
import {messageCallbackType} from "@stomp/stompjs/src/types";
import {IContact, IConversation, IEnvelope, IMessage} from "@src/types";
import useStore from "@src/store/store";
import router from "@src/router";

const useSocketStore = defineStore("socket", () => {
    const authStore = useUserStore()
    const store = useStore()

    function connectionSuccess(frame: IFrame) {
        // eslint-disable-next-line
        stompClient.subscribe('/user/queue/messages', messageReceivedCallback);
        // stompClient.subscribe('/user/queue/receipts', cmdReceivedCallback)
    }

    function cmdReceivedCallback(message: StompMessage) {
        console.debug("CMD RECEIVED", message)
    }

    let messageReceivedCallback: messageCallbackType = (message: StompMessage) => {console.log("Message received:",message)}
    let errorCallback: frameCallbackType = (frame) => {console.error("STOMP error:", frame)}

    const stompClient = new Client();

    function init(onMessageReceivedCallback: messageCallbackType, onErrorCallback: frameCallbackType) {
        messageReceivedCallback = onMessageReceivedCallback
        errorCallback = onErrorCallback
        stompClient.configure({
            brokerURL: import.meta.env.VITE_APP_BACKEND_WS_URL,
            reconnectDelay: 5000,
            debug: function(e) {
                // eslint-disable-next-line
                console.log(e)
            },
            connectHeaders: {
                authorization: 'Bearer ' + authStore.token?.accessToken
            },
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: connectionSuccess,
            onStompError: errorCallback,
            onDisconnect: function(e: any) {
                // eslint-disable-next-line
                console.log("STOMP Disconnect")
                console.error(e)
            },
            onWebSocketError: function(e: any) {
                // eslint-disable-next-line
                console.log("WS Error")
                console.error(e)
            },
        })
        stompClient.activate()
    }


    function newCoupleConversation(contact: IContact) {
        // stompClient.publish({
        //     destination: "/chat/newConversation",
        //     body: JSON.stringify(conversation)
        // })
    }
    function close(options?: {force?: boolean | undefined} | undefined) {
        stompClient.deactivate(options).then(()=>console.log("StompClientClosed")).catch(()=>console.log("StompClientClosed with error"))
    }

    function sendMessage(message: IEnvelope, receiptId: string, callback: frameCallbackType){
        stompClient.watchForReceipt(receiptId, callback);
        stompClient.publish({
            destination: "/chat/message",
            headers: {receipt: receiptId},
            body: JSON.stringify(message),
        })
    }

    return {
        init,
        close,
        sendMessage
    }

})

export default useSocketStore;