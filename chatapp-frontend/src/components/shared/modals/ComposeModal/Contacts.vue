<script setup  lang="ts">
import useStore from "@src/store/store";

import NoContacts from "@src/components/states/empty-states/NoContacts.vue";
import Loading1 from "@src/components/states/loading-states/Loading1.vue";
import SearchInput from "@src/components/ui/inputs/SearchInput.vue";
import ContactItem from "@src/components/shared/blocks/ContactItem.vue";
import ScrollBox from "@src/components/ui/utils/ScrollBox.vue";
import useSocketStore from "@src/store/socket";
import {IContact, IConversation} from "@src/types";
import {parse as uuidParse, stringify as uuidStringify} from "uuid";
import {getUserAsContact} from "@src/utils";
import {useUserStore} from "@src/store/user";

const emit = defineEmits(['goToConversation'])

const store = useStore();
const userStore = useUserStore();

const socketStore = useSocketStore();

function contactSelected(contact: IContact){
  // console.log(contact)
  // socketStore.newCoupleConversation(contact)
  const r1 = uuidParse(contact.id)
  const r2 = uuidParse(userStore.user!.id)
  const r3 = r1.map((v, i, a) => {
    if (i == 6)
      return ((v^r2[i]) & 0x0F) | 0x40
    if (i == 8)
      return ((v^r2[i]) & 0x3F) | 0x80
    return (v^r2[i])
  })
  console.log(r3)
  const conversationId = uuidStringify(r3)
  const existingConversation = store.conversations.filter((v,i,a)=> v.id == conversationId).at(0)
  console.log(conversationId)
  console.log(existingConversation)
  if ( !existingConversation ) {
    const conversation: IConversation = {
      id: uuidStringify(r3),
      type: "couple",
      contacts: [
        contact,
        getUserAsContact(userStore.user!)
      ],
      messages: [],
      draftMessage: ""
    }
    console.log(conversation)
    store.conversations.push(conversation)
  }
  store.activeConversationId = conversationId
  store.activeSidebarComponent = "messages"
  emit('goToConversation')
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
          store.user &&
          store.user.contacts.length > 0
        "
        v-for="(contact, index) in store.user.contacts"
        :key="index"
        :contact="contact"
        @contact-selected="contactSelected"
      />

      <NoContacts vertical v-else />
    </ScrollBox>
  </div>
</template>
