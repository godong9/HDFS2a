PROTO_DIR=src/test/proto
                    JAVA_DIR=target/generated-test-sources/java
                    which cygpath 2> /dev/null
                    if [ $? = 1 ]; then
                      IS_WIN=false
                    else
                      IS_WIN=true
                      WIN_PROTO_DIR=`cygpath --windows $PROTO_DIR`
                      WIN_JAVA_DIR=`cygpath --windows $JAVA_DIR`
                    fi
                    mkdir -p $JAVA_DIR 2> /dev/null
                    for PROTO_FILE in `ls $PROTO_DIR/*.proto 2> /dev/null`
                    do
                        if [ "$IS_WIN" = "true" ]; then
                          protoc -I$WIN_PROTO_DIR --java_out=$WIN_JAVA_DIR $PROTO_FILE
                        else
                          protoc -I$PROTO_DIR --java_out=$JAVA_DIR $PROTO_FILE
                        fi
                    done