
all: javadocs

PACKAGES = seda.sandStorm.api \
           seda.sandStorm.api.internal \
           seda.sandStorm.core \
           seda.sandStorm.main \
           seda.sandStorm.internal \
           seda.sandStorm.lib.aSocket \
           seda.sandStorm.lib.aDisk \
           seda.sandStorm.lib.http \
           seda.sandStorm.lib.Gnutella \
           seda.nbio \
           seda.util 

javadocs:
	javadoc -d . -header '<b>More information:</b> <a href="http://www.cs.berkeley.edu/~mdw/proj/seda/">http://www.cs.berkeley.edu/~mdw/proj/seda/</a>' -windowtitle 'SEDA Javadoc Documentation' -package -author $(PACKAGES)

clean:
	rm -rf *.html seda package-list stylesheet.css
