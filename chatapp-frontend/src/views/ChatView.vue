<template>
  <!-- component -->
  <!-- This is an example component -->
  <h1>Hello</h1>
</template>

<script>
import {Client, StompHeaders} from "@stomp/stompjs";
import { useAuthStore } from "../stores/auth";
import {fetchWrapper} from "@/helpers/fetchWrapper";
// import SockJS from "sockjs-client";

export default {

    setup() {
        const authStore = useAuthStore();
        return { authStore };
    },
    created() {
        this.subscribeToTopic()
    },
    methods: {
        connectionSuccess(frame) {
            const auth = useAuthStore()
            // eslint-disable-next-line
            console.log(frame)
            this.stompClient.subscribe('/canuto', this.onMessageReceived);

            this.stompClient.publish({destination: "/queue/test", headers: {priority: "9"}, body: "Hello, STOMP"} )
        },
        onMessageReceived(payload) {
            // eslint-disable-next-line
            console.log(payload)
            this.queueSize = payload.body
            this.$emit('updated', this.queueSize)
        },
        async subscribeToTopic() {
            const csrfToken = await this.authStore.csrf()
            let connectHeaders = new StompHeaders()
            connectHeaders[csrfToken.headerName] = csrfToken.token
            connectHeaders["Authorization"] = `Bearer ${this.authStore.token?.accessToken}`
            this.stompClient = new Client({
                connectHeaders: connectHeaders,
                brokerURL: import.meta.env.VITE_APP_BACKEND_WS_URL,
                reconnectDelay: 5000,
                debug: function(e) {
                    // eslint-disable-next-line
                    console.log(e)
                },
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000
            });

            this.stompClient.onConnect = this.connectionSuccess
            this.stompClient.onWebSocketClose = function(e) {
                // eslint-disable-next-line
                console.error(e)
            }
            this.stompClient.onDisconnect = function(e) {
                // eslint-disable-next-line
                console.error(e)
            }
            this.stompClient.onStompError = function(e) {
                // eslint-disable-next-line
                console.error(e)
            }

            this.stompClient.activate()
        }
    }
}
</script>

<style>
@media (min-width: 1024px) {
  .about {
    min-height: 100vh;
    display: flex;
    align-items: center;
  }
}
</style>
