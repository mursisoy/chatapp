import { useAuthStore } from '@src/store/auth';
export const fetchWrapper = {
    get: request('GET'),
    post: request('POST'),
    put: request('PUT'),
    delete: request('DELETE')
};

function request(method: string) {
    return (url: string, body?: object) => {

        const requestOptions: RequestInit = {
            method: method,
        };

        const headers = new Headers();

        const auth = authHeader(url)

        if (auth)
            headers.set('Authorization', auth)

        if (body) {
            headers.append('Content-Type','application/json')
            requestOptions.body = JSON.stringify(body);
        }
        requestOptions.headers = headers
        return fetch(url, requestOptions).then(handleResponse);
    }
}

// helper functions

function authHeader(url: string) {
    // return auth header with jwt if user is logged in and request is to the api url
    const { token } = useAuthStore();
    const isLoggedIn = !!token?.accessToken;
    const isApiUrl = url.startsWith(import.meta.env.BACKEND_URL);

    if (isLoggedIn && isApiUrl) {
        return `Bearer ${token.accessToken}`;
    }
}

function handleResponse(response: any) {
    return response.text().then((text: string)=> {
        const data = text && JSON.parse(text);

        if (!response.ok) {
            const { user, logout } = useAuthStore();
            if ([401, 403].includes(response.status) && user) {
                // auto logout if 401 Unauthorized or 403 Forbidden response returned from api
                logout();
            }

            const error = (data && data.message) || response.statusText;
            return Promise.reject(error);
        }

        return data;
    });
}