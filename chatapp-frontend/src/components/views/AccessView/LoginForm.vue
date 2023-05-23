<script setup lang="ts">
import { ref } from "vue";

import Typography from "@src/components/ui/data-display/Typography.vue";
import Button from "@src/components/ui/inputs/Button.vue";
import IconButton from "@src/components/ui/inputs/IconButton.vue";
import TextInput from "@src/components/ui/inputs/TextInput.vue";
import { EyeSlashIcon, EyeIcon } from "@heroicons/vue/24/outline";
import { RouterLink } from "vue-router";
import {useUserStore} from "@src/store/user";
import {IUserLogin, IUserSignUp} from "@src/types";
import router from "@src/router";

const showPassword = ref(false);

const loginFormData = ref<IUserLogin>({
  username: "",
  password: ""
});

const authStore = useUserStore();
const login = async () => {
  await authStore.login(loginFormData.value);
  await router.push('/')
}

const updateLoginFormData = (data: IUserLogin) => {
  loginFormData.value = data
}

</script>

<template>
  <div
    class="p-5 md:basis-1/2 xs:basis-full flex flex-col justify-center items-center"
  >
    <div class="w-full md:px-[26%] xs:px-[10%]">
      <!--header-->
      <div class="mb-6 flex flex-col">
        <img
          src="@src/assets/vectors/logo-gradient.svg"
          class="w-[22px] h-[18px] mb-4 opacity-70"
          alt="bird logo"
        />
        <Typography variant="heading-2" class="mb-4">Welcome back</Typography>
        <Typography variant="body-3" class="text-opacity-75 font-light">
          Create an account a start messaging now!
        </Typography>
      </div>

      <!--form-->
      <form @submit.prevent="login">
      <div class="mb-6">
        <TextInput label="Username" placeholder="Enter your username" :value="loginFormData.username"
                   @input="updateLoginFormData({...loginFormData, username: $event.target.value})" class="mb-5" />
        <TextInput
          label="Password"
          placeholder="Enter your password"
          :value="loginFormData.password"
          @input="updateLoginFormData({...loginFormData, password: $event.target.value})"
          :type="showPassword ? 'text' : 'password'"
          class="pr-[40px]"
        >
          <template v-slot:endAdornment>
            <IconButton
              title="toggle password visibility"
              aria-label="toggle password visibility"
              class="m-[8px] p-2"
              @click="showPassword = !showPassword"
            >
              <EyeSlashIcon
                v-if="showPassword"
                class="w-5 h-5 text-black opacity-50 dark:text-white dark:opacity-60"
              />
              <EyeIcon
                v-else
                class="w-5 h-5 text-black opacity-50 dark:text-white dark:opacity-60"
              />
            </IconButton>
          </template>
        </TextInput>
      </div>

      <!--local controls-->
      <div class="mb-6">
        <Button class="w-full mb-4" type="submit">Sign in</Button>
      </div>
      </form>

      <!--oauth controls-->
      <div>
        <!--bottom text-->
        <div class="flex justify-center">
          <Typography variant="body-2"
            >Don’t have an account ?
            <RouterLink
              to="/access/sign-up/"
              class="text-indigo-400 opacity-100"
            >
              Sign up
            </RouterLink>
          </Typography>
        </div>
      </div>
    </div>
  </div>
</template>
