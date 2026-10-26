set -euo pipefail
api="$1"
paquete="cl.duocuc.segurosbpo.prueba"
actividad="$paquete/cl.duocuc.segurosbpo.MainActivity"
mkdir -p evidencia-apk
adb wait-for-device
adb install -r apk/app-debug.apk
adb shell pm path "$paquete" | tee "evidencia-apk/paquete-$api.txt"
adb shell am start -n "$actividad"
sleep 8
adb shell pidof "$paquete" | tee "evidencia-apk/proceso-$api.txt"
for intento in 1 2 3; do
  adb shell uiautomator dump /sdcard/ventana.xml || true
  adb pull /sdcard/ventana.xml "evidencia-apk/ventana-$api.xml" || true
  if grep -q 'Elige quién eres' "evidencia-apk/ventana-$api.xml" && ! grep -qi "isn.t responding" "evidencia-apk/ventana-$api.xml"; then
    break
  fi
  adb shell am force-stop com.google.android.apps.nexuslauncher || true
  adb shell am force-stop com.android.launcher3 || true
  adb shell input keyevent 4 || true
  adb shell am start -n "$actividad"
  sleep 6
done
grep -q 'Elige quién eres' "evidencia-apk/ventana-$api.xml"
if grep -qi "isn.t responding" "evidencia-apk/ventana-$api.xml"; then
  exit 1
fi
adb exec-out screencap -p > "evidencia-apk/inicio-$api.png"
adb shell am force-stop "$paquete"
adb shell am start -n "$actividad"
sleep 5
adb shell pidof "$paquete"
reinicio_verificado=0
for intento in 1 2 3 4 5; do
  adb shell rm -f /sdcard/ventana-reinicio.xml
  if adb shell uiautomator dump /sdcard/ventana-reinicio.xml &&
     adb pull /sdcard/ventana-reinicio.xml "evidencia-apk/reinicio-$api.xml" &&
     grep -q 'Elige quién eres' "evidencia-apk/reinicio-$api.xml"; then
    reinicio_verificado=1
    break
  fi
  adb shell pidof "$paquete" > /dev/null
  sleep 4
done
test "$reinicio_verificado" -eq 1
