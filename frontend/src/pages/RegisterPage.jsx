import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosConfig';

const RegisterPage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        displayName: '',
        email: '',
        ownerMobile: '',
        password: ''
    });
    const [status, setStatus] = useState({ type: '', message: '' });
    const [isLoading, setIsLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setStatus({ type: '', message: '' });

        try {
            // Adjust the URL path if your controller mapping is different
            const response = await api.post('/auth/register', formData);
            
            setStatus({ type: 'success', message: response.data.message });
            
            // Redirect to login after 2 seconds
            setTimeout(() => {
                navigate('/login');
            }, 2000);
            
        } catch (error) {
            // Catches your custom BadRequestException message
            const errorMessage = error.response?.data?.message || "Registration failed. Please try again.";
            setStatus({ type: 'error', message: errorMessage });
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-50 flex flex-col justify-center items-center p-4 font-sans">
            <div className="w-full max-w-md bg-white rounded-xl shadow-sm border border-slate-200 p-8">
                <div className="text-center mb-8">
                    <h1 className="text-2xl font-bold text-slate-900">Apartment Ledger</h1>
                    <p className="text-sm text-slate-500 mt-1">Register your flat to get started</p>
                </div>

                {status.message && (
                    <div className={`mb-6 p-4 rounded-md text-sm ${status.type === 'error' ? 'bg-red-50 text-red-700 border border-red-200' : 'bg-green-50 text-green-700 border border-green-200'}`}>
                        {status.message}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Display Name</label>
                        <input type="text" name="displayName" value={formData.displayName} onChange={handleChange} required
                            className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Email Address</label>
                        <input type="email" name="email" value={formData.email} onChange={handleChange} required
                            className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Flat Owner's Mobile Number</label>
                        <input type="text" name="ownerMobile" value={formData.ownerMobile} onChange={handleChange} required
                            placeholder="Must match admin records"
                            className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Password</label>
                        <input type="password" name="password" value={formData.password} onChange={handleChange} required
                            className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all" />
                    </div>

                    <button type="submit" disabled={isLoading}
                        className="w-full bg-blue-600 text-white font-medium py-2.5 rounded-lg hover:bg-blue-700 transition-colors disabled:bg-blue-400">
                        {isLoading ? 'Registering...' : 'Register Account'}
                    </button>
                </form>

                <div className="mt-6 text-center text-sm text-slate-600">
                    Already have an account? <Link to="/login" className="text-blue-600 font-medium hover:underline">Log in</Link>
                </div>
            </div>
        </div>
    );
};

export default RegisterPage;
