-- openscape: the "main" realm is the public/friends realm (set `realm = "main"` in .data/server.toml).
-- devMode off (no admin commands, no free ::invadd/::tele), passwords always checked, and accounts
-- are created on first login (require_registration = 0; there is no external registration system).
UPDATE realms
SET
    dev_mode = 0,
    ignore_passwords = 0,
    require_registration = 0,
    auto_assign_display_names = 1,
    login_message = 'Welcome to openscape.'
WHERE name = 'main';
