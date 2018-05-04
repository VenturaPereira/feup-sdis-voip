find . -name "*.java" -print | xargs javac

if [ "$1" = "server" ]; then 
    clear; 
    java ProxyServer 9000; 
fi

if [ "$1" = "phone1" ]; then 
    clear; 
    java Phone alice localhost 9000;
fi

if [ "$1" = "phone2" ]; then 
    clear; 
    java Phone bob localhost 9000;
fi

if [ "$1" = "client" ]; then
    clear; 
    java Client bob localhost;
fi