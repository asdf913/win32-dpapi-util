# win32-dpapi-util

## Command

### cryptProtectData
<pre>java -jar win32-dpapi-util-0.0.1-SNAPSHOT.jar action=cryptProtectData input=12345</pre>
<pre>[main] [INFO ] [Win32DpApiUtil.info(Win32DpApiUtil.java:90)]: AQAAANCMnd8BFdERjHoAwE/Cl+sBAAAADGlfyr9wSU2PAbkwpUQwggAAAAACAAAAAAAQZgAAAAEAACAAAAD7ORd/yssUlQRH4FvQP5ZQGlOtSuyIN+vo4XGxL3uKcAAAAAAOgAAAAAIAACAAAAAtmMd2tel8yjWhJQEZ2opd68OlcatiqsxezPsEDuLm1BAAAAD707sOEkyrWkk3ojetXnimQAAAANtFLv3/ACuWGEBLAGuwhVCedZnIGLg2I3BaQYHMXuq2rYBvSWnn9eueOcOK9JK52x9S3fKNCIgvn/Kr9HTzxjo=</pre>

### cryptUnprotectData
<pre>java -jar win32-dpapi-util-0.0.1-SNAPSHOT.jar action=cryptUnprotectData encoding=base64 input=AQAAANCMnd8BFdERjHoAwE/Cl+sBAAAADGlfyr9wSU2PAbkwpUQwggAAAAACAAAAAAAQZgAAAAEAACAAAAD7ORd/yssUlQRH4FvQP5ZQGlOtSuyIN+vo4XGxL3uKcAAAAAAOgAAAAAIAACAAAAAtmMd2tel8yjWhJQEZ2opd68OlcatiqsxezPsEDuLm1BAAAAD707sOEkyrWkk3ojetXnimQAAAANtFLv3/ACuWGEBLAGuwhVCedZnIGLg2I3BaQYHMXuq2rYBvSWnn9eueOcOK9JK52x9S3fKNCIgvn/Kr9HTzxjo=</pre>
<pre>[main] [INFO ] [Win32DpApiUtil.info(Win32DpApiUtil.java:90)]: 12345</pre>

## Reference

<a href="https://learn.microsoft.com/en-us/windows/win32/api/dpapi">Dpapi.h header - Win32 apps | Microsoft Learn</a>

<img width="704" height="882" alt="image" src="https://github.com/user-attachments/assets/b9bdf308-449d-41e3-8179-5ceb7189ea23" />


<a href="https://learn.microsoft.com/en-us/windows/win32/api/dpapi/nf-dpapi-cryptprotectdata">CryptProtectData function (dpapi.h) - Win32 apps | Microsoft Learn</a>

<a href="https://learn.microsoft.com/en-us/windows/win32/api/dpapi/nf-dpapi-cryptunprotectdata">CryptUnprotectData function (dpapi.h) - Win32 apps | Microsoft Learn</a>
