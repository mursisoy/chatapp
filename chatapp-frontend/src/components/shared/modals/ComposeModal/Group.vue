<script setup lang="ts">
import { computed, ref } from "vue";

import GroupInfo from "@src/components/shared/modals/ComposeModal/GroupInfo.vue";
import GroupMembers from "@src/components/shared/modals/ComposeModal/GroupMembers.vue";
import SlideTransition from "@src/components/ui/transitions/SlideTransition.vue";
import {IConversation, ICreateGroup} from "@src/types";
import router from "@src/router";
import useStore from "@src/store/store";

const emit = defineEmits(['activePageChange']);


const store = useStore();

// used to determine whether to slide left or right
const animation = ref("slide-left");

// name of the active modal page
const activePageName = ref("group-info");

// the active page component
const ActivePage = computed(() => {
  if (activePageName.value === "group-info") return GroupInfo;
  else if (activePageName.value === "group-members") return GroupMembers;
  else return GroupMembers;
});

// event to move between modal pages
const changeActiveTab = (event: { tabName: string; animationName: string }) => {
  animation.value = event.animationName;
  activePageName.value = event.tabName;
};

const createGroupForm = ref<ICreateGroup>({
  name: "",
  contacts: []
});

const updateModelValue = (data: ICreateGroup) => {
  createGroupForm.value = data
}

const createGroup = async () => {
  // console.debug(createGroupForm.value.name)
  if (store.user != undefined && createGroupForm.value.contacts.length > 0) {
    store.createConversation(
        {
          type: "GROUP",
          name: createGroupForm.value.name,
          contacts: createGroupForm.value.contacts.concat(store.user.id)
        }
    ).then((conversation: IConversation) => {
      console.debug(conversation)
      conversation.messages = []
      store.conversations.push(conversation)
      store.activeConversationId = conversation.id
      store.activeSidebarComponent = "messages"
      emit('goToConversation')
    }).catch(error => console.error(error))
  }
}
</script>

<template>
  <div>
    <!--content-->
    <div class="overflow-x-hidden">
      <form @submit.prevent="createGroup">
      <SlideTransition :animation="animation">
        <component
          v-model="createGroupForm"
          v-on:update:modelValue="createGroupForm"
          @active-page-change="changeActiveTab"
          :is="ActivePage"
          :key="activePageName"
        />
      </SlideTransition>
      </form>
    </div>
  </div>
</template>
