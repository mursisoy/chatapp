<script setup lang="ts">
// import { attachments } from "@src/store/defaults";

import Attachment from "@src/components/shared/modals/AttachmentsModal/Attachment.vue";
import Button from "@src/components/ui/inputs/Button.vue";
import TextInput from "@src/components/ui/inputs/TextInput.vue";
import Modal from "@src/components/ui/utils/Modal.vue";
import ScrollBox from "@src/components/ui/utils/ScrollBox.vue";
import {ref} from "vue";
import {IFileUpload} from "@src/types";

const emit = defineEmits(['send-attachment-message'])

const props = defineProps<{
  open: boolean;
  closeModal: () => void;
}>();

const uploadFormData = ref<IFileUpload>({
  file: null,
  caption: ""
});

const onChangeFile = (event: Event) => {
  uploadFormData.value.file = (<HTMLInputElement>event.target).files?.item(0)
}


</script>

<template>
  <Modal :open="props.open" :close-modal="props.closeModal">
    <template v-slot:content>
      <div class="w-[400px] bg-white dark:bg-gray-800 rounded py-6">
        <input type="file" @change="onChangeFile" name="file" ref="attachment"/>
        <!--attachments list-->
<!--        <ScrollBox class="max-h-[140px] overflow-y-scroll">-->
<!--&lt;!&ndash;          <Attachment&ndash;&gt;-->
<!--&lt;!&ndash;            :attachment="attachment"&ndash;&gt;-->
<!--&lt;!&ndash;&lt;!&ndash;            :key="index"&ndash;&gt;&ndash;&gt;-->
<!--&lt;!&ndash;          />&ndash;&gt;-->
<!--        </ScrollBox>-->

        <!--caption button-->
        <div class="px-5 py-6">
          <TextInput placeholder="Caption" type="text"
             :value="uploadFormData.caption"
             @input="uploadFormData.caption = $event.target.value"/>
        </div>

        <!--Action buttons-->
        <div class="flex w-full px-5">
          <div class="grow flex justify-start">
            <Button variant="ghost"> Add </Button>
          </div>

          <Button type="button" variant="ghost" @click="props.closeModal" class="mr-4">
            Cancel
          </Button>

          <Button type="button" @click="$refs.attachment.value = ''; props.closeModal();$emit('send-attachment-message', uploadFormData);"> Send </Button>
        </div>
      </div>
    </template>
  </Modal>
</template>

<style scoped>

</style>
