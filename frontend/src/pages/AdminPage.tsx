import React, { useEffect, useState } from 'react';
import { getUsers, createUser, deleteUser } from '../api/endpoints';
import type { User } from '../types';

const AdminPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [form, setForm] = useState({ username: '', email: '', password: '', role: 'VIEWER' });

  const load = () => getUsers().then((r) => setUsers(r.data));

  useEffect(() => { load(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    await createUser(form);
    setForm({ username: '', email: '', password: '', role: 'VIEWER' });
    load();
  };

  const handleDelete = async (userId: string) => {
    if (confirm('Delete this user?')) {
      await deleteUser(userId);
      load();
    }
  };

  return (
    <div>
      <h2>User Management</h2>

      <form onSubmit={handleCreate} style={{ marginBottom: 24, display: 'flex', gap: 8 }}>
        <input placeholder="Username" value={form.username}
          onChange={(e) => setForm({ ...form, username: e.target.value })} required />
        <input placeholder="Email" value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })} required />
        <input placeholder="Password" type="password" value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })} required />
        <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
          <option value="VIEWER">VIEWER</option>
          <option value="OPERATOR">OPERATOR</option>
          <option value="TENANT_ADMIN">TENANT_ADMIN</option>
          <option value="SUPERADMIN">SUPERADMIN</option>
        </select>
        <button type="submit">Add User</button>
      </form>

      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ borderBottom: '2px solid #333' }}>
            <th style={{ textAlign: 'left', padding: 8 }}>Username</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Email</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Role</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.userId} style={{ borderBottom: '1px solid #ccc' }}>
              <td style={{ padding: 8 }}>{u.username}</td>
              <td style={{ padding: 8 }}>{u.email}</td>
              <td style={{ padding: 8 }}>{u.role}</td>
              <td style={{ padding: 8 }}>
                <button onClick={() => handleDelete(u.userId)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default AdminPage;
