import { createRouter, createWebHistory} from "vue-router";
import AccessView from "@src/components/views/AccessView/AccessView.vue";
import HomeView from "@src/components/views/HomeView/HomeView.vue";
import PasswordResetView from "@src/components/views/PasswordResetView/PasswordResetView.vue";
import {useUserStore} from "@src/store/user";
import useStore from "@src/store/store";

const routes = [
  {
    path: "/",
    name: "Home",
    component: HomeView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: "/conversations/:conversationId",
    name: "Conversation Handler",
    redirect: (to: any) => {
      const store = useStore();
      // the function receives the target route as the argument
      // a relative location doesn't start with `/`
      // or { path: 'profile'}
      store.activeConversationId = to.params.conversationId
      return {name:'Home'}
    },
    meta: {
      requiresAuth: true
    }
  },

  {
    path: "/access/:method/",
    name: "Access",
    component: AccessView,
  },
  {
    path: "/reset/",
    name: "Password Reset",
    component: PasswordResetView,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  // clear alert on route change
  // const alertStore = useAlertStore();
  // alertStore.clear();
  // const publicPages = ['/access/:method/', '/reset'];
  // const authRequired = !publicPages.includes(to.path);
  const authStore = useUserStore();
  if (to.matched.some(record => record.meta.requiresAuth) && !authStore.token?.accessToken) {
    return {name: 'Access', params: {method: 'sign-in'}};
  }
  // redirect to login page if not logged in and trying to access a restricted page

});
export default router;
