const TOKEN_KEY = "jwtNimbusToken";

async function readJson(response) {
    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
        const validation = body.errors ? Object.values(body.errors).join("; ") : "";
        throw new Error(validation || body.detail || `HTTP ${response.status}`);
    }
    return body;
}

async function api(path, options = {}) {
    const headers = {"Content-Type": "application/json", ...(options.headers || {})};
    const token = sessionStorage.getItem(TOKEN_KEY);
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }
    return readJson(await fetch(path, {...options, headers}));
}

function setMessage(element, message, isError = false) {
    if (!element) return;
    element.textContent = message;
    element.classList.toggle("error", isError);
    element.classList.toggle("success", !isError && Boolean(message));
}

const loginForm = document.getElementById("login-form");
if (loginForm) {
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const message = document.getElementById("login-message");
        setMessage(message, "Dang xac thuc...");
        try {
            const result = await api("/auth/login", {
                method: "POST",
                body: JSON.stringify({
                    email: document.getElementById("login-email").value,
                    password: document.getElementById("login-password").value
                })
            });
            sessionStorage.setItem(TOKEN_KEY, result.token);
            window.location.href = "/profile";
        } catch (error) {
            setMessage(message, error.message, true);
        }
    });
}

const signupForm = document.getElementById("signup-form");
if (signupForm) {
    signupForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const message = document.getElementById("signup-message");
        setMessage(message, "Dang tao tai khoan...");
        const email = document.getElementById("signup-email").value;
        const password = document.getElementById("signup-password").value;
        try {
            await api("/auth/signup", {
                method: "POST",
                body: JSON.stringify({
                    fullName: document.getElementById("signup-name").value,
                    email,
                    password,
                    images: null
                })
            });
            document.getElementById("login-email").value = email;
            document.getElementById("login-password").value = password;
            setMessage(message, "Dang ky thanh cong. Hay bam Dang nhap.");
            signupForm.reset();
        } catch (error) {
            setMessage(message, error.message, true);
        }
    });
}

async function loadProfile() {
    if (!document.getElementById("users-body")) return;
    if (!sessionStorage.getItem(TOKEN_KEY)) {
        window.location.href = "/login";
        return;
    }

    const message = document.getElementById("profile-message");
    try {
        const [me, users] = await Promise.all([api("/users/me"), api("/users")]);
        document.getElementById("profile-name").textContent = me.fullName;
        document.getElementById("profile-email").textContent = me.email;
        document.getElementById("profile-role").textContent = me.role;
        document.getElementById("avatar").textContent = me.fullName.trim().charAt(0).toUpperCase();
        document.getElementById("user-count").textContent = users.length;

        const body = document.getElementById("users-body");
        body.replaceChildren(...users.map((user) => {
            const row = document.createElement("tr");
            [user.id, user.fullName, user.email, user.role].forEach((value) => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            });
            return row;
        }));
    } catch (error) {
        sessionStorage.removeItem(TOKEN_KEY);
        setMessage(message, `${error.message}. Dang chuyen ve trang dang nhap...`, true);
        setTimeout(() => window.location.href = "/login", 1200);
    }
}

const logoutButton = document.getElementById("logout");
if (logoutButton) {
    logoutButton.addEventListener("click", () => {
        sessionStorage.removeItem(TOKEN_KEY);
        window.location.href = "/login";
    });
}

loadProfile();
