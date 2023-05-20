<script setup lang="ts">
import { ArrowDownTrayIcon } from "@heroicons/vue/24/outline";
import { PlayIcon } from "@heroicons/vue/24/solid";
import type { Ref } from "vue";
import { computed, ref } from "vue";

import type { IAttachment, IMessage } from "@src/types";

import Carousel from "@src/components/ui/data-display/Carousel/Carousel.vue";
import Typography from "@src/components/ui/data-display/Typography.vue";
import useStore from "@src/store/store";

const store = useStore()

const props = defineProps<{
  message: IMessage;
  self?: boolean;
}>();

const downloadMedia = async () => {
  let filename = '';
  let type = ''
  const file = await store.downloadFile(
      props.message.to,
      props.message.media?.id!
  )
  const blob = await file.blob();
  var url = URL.createObjectURL(blob);
  var a = document.createElement('a');
  a.href = url;
  a.download = props.message.media?.name!;
  document.body.appendChild(a); // we need to append the element to the dom -> otherwise it will not work in firefox
  a.click();
  a.remove();  //afterwards we remove the element again
}

</script>

<template>
  <div>
    <div class="flex">
      <div
        class="mr-2 flex items-end"
      >
        <!--file-->
          <div class="flex">
            <!--download button / icons-->
            <button
              c
              class="w-8 h-8 mr-4 flex justify-center rounded-full outline-none items-center duration-200"
              :class="
                props.self
                  ? ['bg-indigo-300']
                  : [
                      'bg-indigo-50',
                      'hover:bg-indigo-100',
                      'active:bg-indigo-200',
                      'dark:bg-gray-400',
                      'dark:hover:bg-gray-300',
                      'dark:focus:bg-gray-300',
                      'dark:active:bg-gray-200',
                    ]
              "
            >
              <ArrowDownTrayIcon
                  @click="downloadMedia"
                class="stroke-2 h-5 w-5"
                :class="
                  props.self
                    ? ['text-white']
                    : ['text-blue-500', 'dark:text-gray-50']
                "
              />
            </button>

            <div class="flex flex-col justify-center">
              <Typography
                variant="heading-2"
                :no-color="true"
                class="mb-3"
                :class="
                  props.self
                    ? ['text-black opacity-50 dark:text-white dark:opacity-70 ']
                    : [
                        'text-black',
                        'opacity-50',
                        'dark:text-white',
                        'dark:opacity-70',
                      ]
                "
              >
                {{ message.media.name }}</Typography
              >

              <Typography
                variant="body-2"
                :no-color="true"
                :class="
                  props.self
                    ? ['text-black opacity-60 dark:text-white dark:opacity-70']
                    : [
                        'text-black',
                        'opacity-50',
                        'dark:text-white',
                        'dark:opacity-70',
                      ]
                "
              >
                {{ message.media.size }}
              </Typography>
            </div>
          </div>
      </div>

    </div>
  </div>
</template>
