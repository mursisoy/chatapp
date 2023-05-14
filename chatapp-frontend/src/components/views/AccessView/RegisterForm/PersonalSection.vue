<script setup lang="ts">
import Button from "@src/components/ui/inputs/Button.vue";
import TextInput from "@src/components/ui/inputs/TextInput.vue";
import Typography from "@src/components/ui/data-display/Typography.vue";
import {IUserSignUp} from "@src/types";
const props = defineProps<{
  modelValue: IUserSignUp
}>()
const emit = defineEmits(['update:modelValue','activeSectionChange']);
</script>

<template>
  <div>
    <!--form-->
    <div class="mb-5">
      <TextInput label="Username" placeholder="Enter your username" class="mb-5" :value="modelValue.username"
                 @input="$emit('update:modelValue', {...modelValue, email: $event.target.value})"/>
    </div>

    <!--local controls-->
    <div class="mb-6">
      <Button
        class="w-full mb-4"
        type="button"
        @click="
          $emit('active-section-change', {
            sectionName: 'password-section',
            animationName: 'slide-left',
          })
        "
        >Next</Button
      >
    </div>

  </div>
</template>
