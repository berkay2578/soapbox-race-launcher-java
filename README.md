Latest release: https://github.com/berkay2578/soapbox-race-launcher/releases/tag/v0.2.0-SNAPSHOT

SoapboxHill - Client Launcher
-
A cross-platform client launcher developed for soapbox-server with SWT and Apache Maven.

**To launch**, you need JRE8. Mac users need to specifiy '-XstartOnFirstThread'. (google it)

**To build**, use:
```mvn clean compile assembly:single -PprofileName -DskipDevelopment```
or use the included bash script.

Checklist:
- [x] ~~Login/Register.~~
- [x] ~~Launch client with referencing the selected server.~~
- [x] ~~Server selection dialog with working settings file.~~
- [x] ~~Auto-Login with working settings file.~~
- [x] ~~Client path selection with working settings file.~~
- [x] ~~Basic server info display (URL, HttpPort).~~
- [ ] Request in server selection dialog to get 'serverBanner.png' and 'serverInfo.html'.
- [ ] Support for caching those files.
- [ ] Finish other settings.
- [ ] Finish "server details".
- [ ] Automagically find client executable.
- [ ] Create .dmg for Mac OS users. (see you fellas in 2116!)
