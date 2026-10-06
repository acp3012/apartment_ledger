import axios from 'axios';

const api = axios.create({
    // Adjust the port if your Spring Boot runs on 8081 or something else
    baseURL: 'http://localhost:8081/al/api/v1', 
    headers: {
        'Content-Type': 'application/json',
    },
});

export default api;
