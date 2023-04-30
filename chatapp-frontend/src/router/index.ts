import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import {useAuthStore} from "@/stores/auth";
import Login from "@/views/Login.vue";
import Logout from "@/views/Logout.vue";
import Register from "@/views/Register.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/chat/all'
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue')
    },
    { path: '/chat/:id', name:'chat',
      component: () => import('../views/ChatView.vue') },
    { path: '/login', component: Login },
    { path: '/logout', component: Logout },
    { path: '/register', component: Register }
  ]
})

router.beforeEach(async (to) => {
  // clear alert on route change
  // const alertStore = useAlertStore();
  // alertStore.clear();

  // redirect to login page if not logged in and trying to access a restricted page
  const publicPages = ['/login', '/register'];
  const authRequired = !publicPages.includes(to.path);
  const authStore = useAuthStore();
  console.log(authRequired)
  console.log(authStore.token)
  if (authRequired && !authStore.token) {
    return '/login';
  }
});
export default router
