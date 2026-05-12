/**
 * json-server middleware — handle POST endpoints that json-server can't.
 *
 * Background:
 *   json-server treats POST to a singleton route as "create entity" and
 *   ECHOES the request body (and mutates db.json). That breaks 2 endpoints
 *   for us: /api/auth/login and /api/recommendations/:id/acknowledge.
 *
 * Solution:
 *   - These 2 paths are NOT in routes.json, so json-server's rewriter
 *     leaves them untouched.
 *   - This middleware (which runs BEFORE the rewriter & router for paths
 *     it owns) intercepts them, reads db.json, and returns a static
 *     mock response.
 *
 * Pattern is "send and don't call next()" → request never reaches router →
 * db.json stays clean.
 */
const fs = require('fs');
const path = require('path');

const DB_PATH = path.join(__dirname, 'db.json');

function readDb() {
    return JSON.parse(fs.readFileSync(DB_PATH, 'utf-8'));
}

function authError(reqPath, msg) {
    return {
        timestamp: new Date().toISOString(),
        status: 401,
        error: 'Unauthorized',
        message: msg,
        path: reqPath
    };
}

module.exports = (req, res, next) => {
    const reqPath = (req.originalUrl || req.url || '').split('?')[0];

    // --- POST /api/auth/login → return static login response from db.auth_login ---
    if (req.method === 'POST' && reqPath === '/api/auth/login') {
        const db = readDb();
        const { username, password } = req.body || {};

        const validUsers = { admin: 'admin', manager: 'manager', viewer: 'viewer' };
        if (!username || validUsers[username] !== password) {
            return res.status(401).json(authError(reqPath,
                'Invalid credentials. Seed users: admin/admin, manager/manager, viewer/viewer.'));
        }

        const base = db.auth_login;
        const user = {
            ...base.user,
            id: username === 'admin' ? 1 : username === 'manager' ? 2 : 3,
            username: username,
            fullName: username === 'admin' ? 'Administrator'
                    : username === 'manager' ? 'Operations Manager'
                    : 'Read-Only Viewer',
            email: `${username}@ves.local`,
            role: username.toUpperCase()
        };
        return res.status(200).json({ ...base, user });
    }

    // --- POST /api/recommendations/:id/acknowledge → ack response with ID echo ---
    const ackMatch = reqPath.match(/^\/api\/recommendations\/(\d+)\/acknowledge$/);
    if (req.method === 'POST' && ackMatch) {
        const db = readDb();
        const id = parseInt(ackMatch[1], 10);
        const note = (req.body && req.body.note) || null;
        return res.status(200).json({
            ...db.ack_response,
            id: id,
            acknowledgedAt: new Date().toISOString(),
            note: note
        });
    }

    next();
};
