Latest release: https://github.com/berkay2578/soapbox-race-launcher/releases/tag/v0.1.0-SNAPSHOT

SoapboxHill - Server Launcher
-
A cross-platform server launcher developed with SWT and Apache Maven.

**To launch**, you need JRE8. Mac users need to specifiy '-XstartOnFirstThread'. (google it)

**To build**, use:
'mvn clean compile assembly:single -PprofileName'
Example:
'mvn clean compile assembly:single -Px64windows'

Checklist:
- [x] Login/Register.
- [x] Server selection dialog with working settings file.
- [x] Auto-Login with working settings file. 
- [x] NFS: World path selection with working settings file.
- [x] Basic server info display (URL, HttpPort).
- [ ] Request in server selection dialog to get 'serverBanner.png' and 'serverInfo.html'.
- [ ] Support for caching those files.
- [ ] Finish other settings.
- [ ] Finish "server details".
- [ ] Automagically find nfsw.exe.
- [ ] Create .dmg for Mac OS users. (see you fellas in 2116!)
