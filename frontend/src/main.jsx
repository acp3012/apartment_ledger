import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import "./index.css";
import App from "./App.jsx";
import { AuthProvider } from "./context/AuthContext.jsx";
import { PeriodProvider } from './context/PeriodContext'

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <AuthProvider>
      <PeriodProvider>
      <App />
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop
        closeOnClick
        pauseOnHover
      />
      </PeriodProvider>
    </AuthProvider>
  </StrictMode>,
);
