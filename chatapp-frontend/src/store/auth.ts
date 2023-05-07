import { defineStore } from "pinia";
import router from "@src/router";
import {fetchWrapper} from "@src/helpers/fetchWrapper";
import jwt_decode from "jwt-decode";
import {IUserSignUp} from "@src/types";

interface  State {
    user: any | null,
    token: TokenResponse | null
}
interface TokenResponse {
    accessToken: string,
    expiresAt: bigint,
    type: string
}

interface CsrfTokenResponse {
    token: string,
    parameterName: string,
    headerName: string
}
export const useAuthStore = defineStore("auth", {
    state: (): State => ({
        user: localStorage.getItem('user')? JSON.parse( localStorage.getItem('user') || "{}") : null,
        token: localStorage.getItem('token')? JSON.parse( localStorage.getItem('token') || "{}") : null,
    }),


    actions: {
        async fetchUser() {
            const res = await fetchWrapper.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/auth/user`);

            this.user = res;

            // store user details and jwt in local storage to keep user logged in between page refreshes
            localStorage.setItem('user', JSON.stringify(this.user));

            // redirect to previous url or default to home page
            await router.push('/chat/all');
        },
        async register(user: IUserSignUp) {
            const res = await fetchWrapper.post(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/auth/register`,
                user
            );

            this.token = res
            this.user = jwt_decode(<string>this.token?.accessToken)
            localStorage.setItem('token', JSON.stringify(this.token))
            localStorage.setItem('user', JSON.stringify(this.user))
            // await this.fetchUser()
        },
        async login(username: string, password: string) {
            const res = await fetchWrapper.post(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/auth/login`,
                { username, password });
            this.token = res;
            console.log(this.token?.accessToken)
            this.user = jwt_decode(<string>this.token?.accessToken)
            localStorage.setItem('token', JSON.stringify(this.token))
            localStorage.setItem('user', JSON.stringify(this.user))
            // await this.fetchUser()
            await router.push('/chat/all')
        },
        async csrf(): Promise<CsrfTokenResponse>{
            const csrfToken = await fetchWrapper.get(`${import.meta.env.VITE_APP_BACKEND_URL}/csrf`);
            return csrfToken
        },
        logout() {
            this.user = null
            this.token = null
            localStorage.removeItem('user')
            localStorage.removeItem('token')
            router.push('/login')
        }
    },
});