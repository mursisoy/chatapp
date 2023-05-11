import { defineStore } from "pinia";
import router from "@src/router";
import {fetchWrapper} from "@src/helpers/fetchWrapper";
import jwt_decode, {JwtPayload} from "jwt-decode";
import {IContact, IUser, IUserLogin, IUserSignUp} from "@src/types";

interface  State {
    user: IUser | undefined,
    token: TokenResponse | undefined
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

interface IDecodedJwt extends JwtPayload{
    username: string,
    email: string,
    firstName: string,
    lastName: string,
    role: string
}
export const useUserStore = defineStore("auth", {
    state: (): State => ({
        user: localStorage.getItem('user') ? JSON.parse( localStorage.getItem('user') || "{}") : undefined,
        token: localStorage.getItem('token') ? JSON.parse( localStorage.getItem('token') || "{}") : undefined,
    }),
    actions: {
        async updateStore(token: TokenResponse) {
            this.token = token
            let decoded_jwt : IDecodedJwt= jwt_decode(<string>this.token?.accessToken)
            console.log(decoded_jwt)
            this.user = {
                id: decoded_jwt.username,
                firstName: decoded_jwt.firstName,
                lastName: decoded_jwt.lastName,
                email: decoded_jwt.email,
                role: decoded_jwt.role,
                avatar: "",
                lastSeen: new Date(),
                contacts: await this.getContacts()
            }
            this.user!.contacts = await this.getContacts()
            localStorage.setItem('token', JSON.stringify(this.token))
            localStorage.setItem('user', JSON.stringify(this.user))
        },
        async fetchUser() {
            const res = await fetchWrapper.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/auth/user`);
            this.user = res;
            localStorage.setItem('user', JSON.stringify(this.user));
        },
        async register(user: IUserSignUp) {
            const res = await fetchWrapper.post(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/auth/register`,
                user
            );
            await this.updateStore(res)
            // await this.fetchUser()
        },
        async login(user: IUserLogin) {
            const res = await fetchWrapper.post(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/auth/login`,
                user);
            await this.updateStore(res)
        },
        async csrf(): Promise<CsrfTokenResponse>{
            const csrfToken = await fetchWrapper.get(`${import.meta.env.VITE_APP_BACKEND_URL}/csrf`);
            return csrfToken
        },
        logout() {
            this.user = undefined
            this.token = undefined
            localStorage.removeItem('user')
            localStorage.removeItem('token')
            localStorage.removeItem('chat')
        },
        async getContacts(): Promise<IContact[]> {
            const res = await fetchWrapper.get(`${import.meta.env.VITE_APP_BACKEND_URL}/api/v1/chat/contacts`)
            return res.contacts
        }
    },
});