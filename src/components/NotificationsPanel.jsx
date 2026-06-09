import React, { useEffect, useState } from 'react';
import { collection, query, orderBy, limit, onSnapshot, doc, updateDoc } from 'firebase/firestore';
import { db } from '../firebase';
import { Bell, Heart } from 'lucide-react';

const NotificationsPanel = ({ userId }) => {
    const [notifications, setNotifications] = useState([]);

    useEffect(() => {
        if (!userId) return;

        const q = query(
            collection(db, "users", userId, "notifications"),
            orderBy("timestamp", "desc"),
            limit(5)
        );

        const unsubscribe = onSnapshot(q, (snapshot) => {
            const notifs = snapshot.docs.map(doc => ({
                id: doc.id,
                ...doc.data()
            }));
            setNotifications(notifs);
        });

        return () => unsubscribe();
    }, [userId]);

    const markAsRead = async (notifId) => {
        // Optional: mark as read logic
        /*
        const notifRef = doc(db, "users", userId, "notifications", notifId);
        await updateDoc(notifRef, { read: true });
        */
    };

    if (notifications.length === 0) return null;

    return (
        <div className="notifications-panel fade-in">
            <div className="panel-header">
                <Bell size={18} className="panel-icon" />
                <h4>Novedades</h4>
            </div>
            <div className="notifications-list">
                {notifications.map(notif => (
                    <div key={notif.id} className="notification-item">
                        {notif.type === 'support' && <Heart size={16} className="notif-icon-support" />}
                        <div className="notif-content">
                            <p>{notif.message}</p>
                            <span className="notif-time">{new Date(notif.timestamp).toLocaleDateString()}</span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default NotificationsPanel;
