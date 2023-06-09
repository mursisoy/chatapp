<script setup lang="ts">
import type { IConversation, IMessage } from "@src/types";
import type { Ref } from "vue";
import { inject, onMounted, ref } from "vue";

import useStore from "@src/store/store";

import Message from "@src/components/views/HomeView/Chat/ChatMiddle/Message/Message.vue";
import TimelineDivider from "@src/components/views/HomeView/Chat/ChatMiddle/TimelineDivider.vue";

const props = defineProps<{
  handleSelectMessage: (messageId: string) => void;
  handleDeselectMessage: (messageId: string) => void;
  selectedMessages: string[];
}>();

const store = useStore();

const container: Ref<HTMLElement | null> = ref(null);

const activeConversation = <Ref<IConversation>>inject("activeConversation");

// checks whether the previous message was sent by the same user.
const isFollowUp = (index: number, previousIndex: number): boolean => {
  if (previousIndex < 0) {
    return false;
  } else {
    if (activeConversation.value.messages ) {
      let previousSender = activeConversation.value.messages[previousIndex].from;
      let currentSender = activeConversation.value.messages[index].from;
      return previousSender === currentSender;
    }
    return false;
  }
};

// checks whether the message is sent by the authenticated user.
const isSelf = (message: IMessage): boolean => {
  return Boolean(store.user && message.from === store.user.username);
};

// checks wether the new message has been sent in a new day or not.
const renderDivider = (index: number, previousIndex: number): boolean => {
  if (index === 3) {
    return true;
  } else {
    return false;
  }
};

// scroll messages to bottom.
onMounted(() => {
  (container.value as HTMLElement).scrollTop = (
    container.value as HTMLElement
  ).scrollHeight;
});
</script>

<template>
  <div
    ref="container"
    class="grow px-5 py-5 flex flex-col overflow-y-scroll scrollbar-hidden"
  >
    <div
      v-if="store.status !== 'loading'"
      v-for="(message, index) in activeConversation.messages"
      :key="index"
    >
      <TimelineDivider v-if="renderDivider(index, index - 1)" />

      <Message
        :message="message"
        :self="isSelf(message)"
        :follow-up="isFollowUp(index, index - 1)"
        :divider="renderDivider(index, index - 1)"
        :selected="props.selectedMessages.includes(message.id)"
        :handle-select-message="handleSelectMessage"
        :handle-deselect-message="handleDeselectMessage"
      />
    </div>
  </div>
</template>
