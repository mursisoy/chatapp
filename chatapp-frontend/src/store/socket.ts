import { defineStore } from "pinia";
import {useUserStore} from "@src/store/user";
import {Client, IFrame, IMessage as StompMessage} from "@stomp/stompjs";
import {messageCallbackType} from "@stomp/stompjs/src/types";
import {IContact, IConversation, IEnvelope, IMessage} from "@src/types";
import { parse as uuidParse, stringify as uuidStringify } from 'uuid';
import {getUserAsContact} from "@src/utils";
import useStore from "@src/store/store";

const useSocketStore = defineStore("socket", () => {
    const authStore = useUserStore()
    const store = useStore()

    function connectionSuccess(frame: IFrame) {
        // eslint-disable-next-line
        stompClient.subscribe('/user/queue/messages', messageReceivedCallback);
    }

    let messageReceivedCallback: messageCallbackType = (message) => {console.log(message)}

    const stompClient = new Client({
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
        onStompError: function(e: any) {
            // eslint-disable-next-line
            console.error(e)
        }
    });

    function init(onMessageReceivedCallback: messageCallbackType) {
        messageReceivedCallback = onMessageReceivedCallback
        stompClient.activate()
    }

    function newCoupleConversation(contact: IContact) {


        // stompClient.publish({
        //     destination: "/chat/newConversation",
        //     body: JSON.stringify(conversation)
        // })
    }

    function close() {
        stompClient.deactivate().then(()=>console.log("StompClientClosed")).catch(()=>console.log("StompClientClosed with error"))
    }

    function sendMessage(message: IEnvelope){
        stompClient.publish({
            destination: "/chat/message",
            body: JSON.stringify(message)
        })
    }

    return {
        init,
        sendMessage
    }

})

export default useSocketStore;