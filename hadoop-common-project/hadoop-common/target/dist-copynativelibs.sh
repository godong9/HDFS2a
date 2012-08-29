which cygpath 2> /dev/null
                      if [ $? = 1 ]; then
                        BUILD_DIR="/home/godong/Project/HDFS2a/hadoop-common-project/hadoop-common/target"
                      else
                        BUILD_DIR=`cygpath --unix '/home/godong/Project/HDFS2a/hadoop-common-project/hadoop-common/target'`
                      fi
                      TAR='tar cf -'
                      UNTAR='tar xfBp -'
                      LIB_DIR="${BUILD_DIR}/native/target/usr/local/lib"
                      if [ -d ${LIB_DIR} ] ; then
                        TARGET_DIR="${BUILD_DIR}/hadoop-common-2.0.1-SNAPSHOT/lib/native"
                        mkdir -p ${TARGET_DIR}
                        cd ${LIB_DIR}
                        $TAR lib* | (cd ${TARGET_DIR}/; $UNTAR)
                        if [ "false" = "true" ] ; then
                          cd ${snappy.lib}
                          $TAR *snappy* | (cd ${TARGET_DIR}/; $UNTAR)
                        fi
                      fi