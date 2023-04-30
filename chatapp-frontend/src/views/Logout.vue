<template>
    <div></div>
</template>

<script>
import { useAuthStore } from "../stores/auth";
import router from "@/router";
export default {
    setup() {
        const authStore = useAuthStore();
        return { authStore };
    },
    created() {
        this.authStore.logout()
    }
};
</script>

<style scoped>

</style>