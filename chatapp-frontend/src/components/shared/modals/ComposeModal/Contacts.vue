<script setup  lang="ts">
import useStore from "@src/store/store";

import NoContacts from "@src/components/states/empty-states/NoContacts.vue";
import Loading1 from "@src/components/states/loading-states/Loading1.vue";
import SearchInput from "@src/components/ui/inputs/SearchInput.vue";
import ContactItem from "@src/components/shared/blocks/ContactItem.vue";
import ScrollBox from "@src/components/ui/utils/ScrollBox.vue";
import useSocketStore from "@src/store/socket";
import {IContact, IConversation, IMessage} from "@src/types";
import {useUserStore} from "@src/store/user";
import {computed} from "vue";

const emit = defineEmits(['goToConversation'])

const store = useStore();
const userStore = useUserStore();

const socketStore = useSocketStore();

const filteredContacts = computed(() => {
  return store.contacts?.filter(contact => contact.username != store.user?.username)
})
function contactSelected(contact: IContact){
  if (store.user != undefined) {
    store.createConversation(
        {
          type: "COUPLE",
          contacts: [
            contact.id,
            store.user.id
          ]
        }
    ).then((conversation: IConversation) => {
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
  <div class="pb-6">
    <!--search-->
    <div class="mx-5 mb-5">
      <SearchInput />
    </div>

    <!--contacts-->
    <ScrollBox class="overflow-y-scroll max-h-[200px]">
      <Loading1
        v-if="store.status === 'loading' || store.delayLoading"
        v-for="item in 3"
      />

      <ContactItem
        v-else-if="
          store.status === 'success' &&
          !store.delayLoading &&
          store.contacts?.length > 0
        "
        v-for="(contact, index) in filteredContacts"
        :key="index"
        :contact="contact"
        @contact-selected="contactSelected"
      />

      <NoContacts vertical v-else />
    </ScrollBox>
  </div>
</template>
