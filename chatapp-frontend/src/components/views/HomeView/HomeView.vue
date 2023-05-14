<script setup lang="ts">
import { computed, onMounted } from "vue";
import {Client, IFrame, IMessage as StompMessage} from "@stomp/stompjs";

import useStore from "@src/store/store";
import {useUserStore} from "@src/store/user";
import useSocketStore from "@src/store/socket";
import Chat from "@src/components/views/HomeView/Chat/Chat.vue";
import Navigation from "@src/components/views/HomeView/Navigation/Navigation.vue";
import Sidebar from "@src/components/views/HomeView/Sidebar/Sidebar.vue";
import NoChatSelected from "@src/components/states/empty-states/NoChatSelected.vue";
import Loading3 from "@src/components/states/loading-states/Loading3.vue";
import FadeTransition from "@src/components/ui/transitions/FadeTransition.vue";
import router from "@src/router";
import {getConversationIndex} from "@src/utils";
import {IMessage} from "@src/types";

const store = useStore();
const authStore = useUserStore();
const socketStore = useSocketStore()

// the active chat component or loading component.
const activeChatComponent = computed(() => {
  if (store.status === "loading" || store.delayLoading) {
    return Loading3;
  } else if (store.activeConversationId) {
    return Chat;
  } else {
    return NoChatSelected;
  }
});

function onMessageReceived(message: StompMessage) {
  console.log(message)
  if (message.body) {
    const conversationMessage: IMessage = JSON.parse(message.body)
    if(!addMessageToConversation(conversationMessage)) {
      store.updateConversation(conversationMessage.to).then(result =>
        addMessageToConversation(conversationMessage)
      )
    }
  }
}
function addMessageToConversation(conversationMessage: IMessage): Boolean {
  const index = getConversationIndex(conversationMessage.to);
  if (index == undefined)
    return false
  if (store.conversations[index].messages == undefined) {
    store.conversations[index].messages = []
  }
  store.conversations[index].messages.push(conversationMessage)
  return true
}

function onError(message: IFrame) {
  console.error("Socket error: ", message)
  socketStore.close({force: true})
  authStore.logout()
  router.push('/access/sign-in')
}

onMounted(() => {

  store.status = "loading";
  setTimeout(() => {
    store.delayLoading = false;
  });
  Promise.all([
    store.updateContacts(),
    store.updateConversations()
  ]).then(()=>{
    store.status = 'success'
  })
  store.$patch({
    user: authStore.user,
    // notifications: request.data.notifications,
    // archivedConversations: request.data.archivedConversations,
  });
  socketStore.init(onMessageReceived, onError);
})
</script>

<template>
  <KeepAlive>
    <div
      class="xs:relative md:static h-full flex xs:flex-col md:flex-row overflow-hidden"
    >
      <!--navigation-bar-->
      <Navigation class="xs:order-1 md:-order-none" />
      <!--sidebar-->
      <Sidebar
        class="xs:grow-1 md:grow-0 xs:overflow-y-scroll md:overflow-visible scrollbar-hidden"
      />
      <!--chat-->
      <div
        id="mainContent"
        class="xs:absolute xs:z-10 md:static grow h-full xs:w-full md:w-fit scrollbar-hidden bg-white dark:bg-gray-800 transition-all duration-500"
        :class="
          store.conversationOpen === 'open'
            ? ['xs:left-[0px]', 'xs:static']
            : ['xs:left-[1000px]']
        "
        role="region"
      >
        <FadeTransition name="fade" mode="out-in">
          <component
            :is="activeChatComponent"
            :key="store.activeConversationId"
          />
        </FadeTransition>
      </div>
    </div>
  </KeepAlive>
</template>
