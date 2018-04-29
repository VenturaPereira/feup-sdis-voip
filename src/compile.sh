find . -name "*.java" -print | xargs javac

if [ "$1" = "server" ]; then clear; java ProxyServer 9000; fi
if [ "$1" = "client" ]; then clear; java Phone bob_newby localhost 9000; fi
