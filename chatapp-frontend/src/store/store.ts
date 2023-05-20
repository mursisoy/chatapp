import { defineStore } from "pinia";
import type { Ref } from "vue";
import { computed, ref } from "vue";

import defaults from "@src/store/defaults";
import {useUserStore} from "@src/store/user";

import type {
  IConversation,
  IContactGroup,
  IUser,
  INotification,
  ICall,
  ISettings,
  IEmoji,
  IContact
} from "@src/types";
import {fetchWrapper} from "@src/helpers/fetchWrapper";
import {getConversationIndex} from "@src/utils";

const useStore = defineStore("chat", () => {

  const authStore = useUserStore()
  // local storage
  const storage = JSON.parse(localStorage.getItem("chat") || "{}");

  // app status refs
  const status = ref("idle");

  // app data refs
  // data refs
  const user: Ref<IUser | undefined> = ref(authStore.user);
  const conversations: Ref<IConversation[]> = ref(defaults.conversations || []);
  const notifications: Ref<INotification[]> = ref(defaults.notifications || []);
  const archivedConversations: Ref<IConversation[]> = ref(
    defaults.archive || []
  );
  const calls: Ref<ICall[]> = ref(defaults.calls || []);
  const settings: Ref<ISettings> = ref(
    storage.settings || defaults.defaultSettings
  );
  const activeCall: Ref<ICall | undefined> = ref(defaults.activeCall);
  const recentEmoji: Ref<IEmoji[]> = ref(storage.recentEmoji || []);
  const emojiSkinTone: Ref<string> = ref(storage.emojiSkinTone || "neutral");

  // ui refs
  const activeSidebarComponent: Ref<string> = ref(
    storage.activeSidebarComponent || "messages"
  );
  const delayLoading = ref(true);
  const activeConversationId: Ref<string | undefined> = ref(undefined);
  const conversationOpen: Ref<string | undefined> = ref(
    storage.conversationOpen
  );
  const callMinimized = ref(false);
  const openVoiceCall = ref(false);

  const contacts: Ref<IContact[] | undefined > = ref( undefined)

  // contacts grouped alphabetically.
  const contactGroups: Ref<IContactGroup[] | undefined> = computed(() => {
      if (contacts.value != undefined) {
        let sortedContacts = contacts.value ;
        sortedContacts.sort();

        let groups: IContactGroup[] = [];
        let currentLetter: string = "";
        let groupNames: string[] = [];

        // create an array of letter for every different sort level.
        for (let contact of sortedContacts) {
          // if the first letter is different create a new group.
          if (contact.username[0].toUpperCase() !== currentLetter) {
            currentLetter = contact.username[0].toUpperCase();
            groupNames.push(currentLetter);
          }
        }

        // create an array that groups contact names based on the first letter;
        for (let groupName of groupNames) {
          let group: IContactGroup = {letter: groupName, contacts: []};
          for (let contact of sortedContacts) {
            if (contact.username[0].toUpperCase() === groupName) {
              group.contacts.push(contact);
            }
          }
          groups.push(group);
        }
        return groups;
      }
  });

  const getStatus = computed(() => status);

  async function createConversation(conversationRequest: any) {
    return fetchWrapper.post(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/chat/conversations`,
        conversationRequest
    )
  }

  async function uploadFile(conversationId: string, fileUploadRequest: any) {
    return fetchWrapper.multipart(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/chat/conversations/${conversationId}/files`,
        fileUploadRequest
    )
  }
  // async getContacts(): Promise<IContact[]> {
  //   const res = await fetchWrapper.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/chat/contacts`)
  //   return res.contacts
  // }
  async function updateContacts() {
    const res = await fetchWrapper.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/chat/contacts`)
    contacts.value = res.contacts
    return res.contacts
  }

  async function updateConversations() {
    const res = await fetchWrapper.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/chat/conversations`)
    console.debug("Conversations updated")
    res.conversations?.forEach( (conversation: IConversation) => {
      const index = getConversationIndex(conversation.id);
      if (index == null) {
        conversations.value.push(conversation)
      } else {
        conversations.value[index] = {...conversations.value[index] ,...conversation}
      }
    })
    return res.conversations
  }

  async function updateConversation(conversationId: string) {
    const res = await fetchWrapper.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/chat/conversations/${conversationId}`)
    const conversation: IConversation = res.conversations[0]
    const index = getConversationIndex(conversation.id);
    if (index == null) {
      conversations.value.push(conversation)
    } else {
      conversations.value[index] = {...conversations.value[index] ,...conversation}
    }
  }

  async function deleteConversation(conversationId: string) {
    const index = getConversationIndex(conversationId);
    return await fetchWrapper.delete(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/chat/conversations/${conversationId}`, undefined,false)
        .then(response => {
          if (response.status == 200) {
            if (index != null) {
              activeConversationId.value = undefined
              conversations.value.splice(index,1)
            }
          }
        })
  }

  async function downloadFile(conversationId: string, fileId: string) {
    return fetchWrapper.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/chat/conversations/${conversationId}/files/${fileId}`)
  }

  async function updateConversationContacts(conversationId: string, request: any) {
      return fetchWrapper.patch(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/chat/conversations/${conversationId}/contacts`, request)
        .then(response => {
          const index = getConversationIndex(conversationId);
          if (index == null) {
            conversations.value.push(response)
          } else {
            conversations.value[index] = {...conversations.value[index] ,...response}
            activeConversationId.value = conversationId
          }
          return response
        })
  }

  return {
    // status refs
    status,
    getStatus,

    // data refs
    user,
    contacts,
    conversations,
    contactGroups,
    notifications,
    archivedConversations,
    calls,
    settings,
    activeCall,
    recentEmoji,
    emojiSkinTone,

    // ui refs
    activeSidebarComponent,
    delayLoading,
    activeConversationId,
    conversationOpen,
    callMinimized,
    openVoiceCall,

    // functions
    createConversation,
    updateContacts,
    updateConversations,
    updateConversation,
    uploadFile,
    downloadFile,
    deleteConversation,
    updateConversationContacts
  };
});

export default useStore;
