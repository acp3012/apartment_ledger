 import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite"; // <-- This must be imported

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
 server: {
  allowedHosts: [".trycloudflare.com"],
  proxy: {
    "^/api/v1": {
      target: "http://localhost:8081",
      changeOrigin: true,
    },
  },
},
});
