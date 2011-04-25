#just a test...

ROOT=./

include build-config.j2me

SRCROOT=$(ROOT)/src

#this is the main build target.

clean:
	cd $(SRCROOT)/org/bouncycastle/crypto;$(MAKE) -f makefile clean
	cd $(SRCROOT)/org/bouncycastle/crypto/digests;$(MAKE) -f makefile clean
	cd $(SRCROOT)/gov/nist/siplite/;$(MAKE) -f makefile clean
	cd $(SRCROOT)/gov/nist/siplite/stack;$(MAKE) -f makefile clean
	cd $(SRCROOT)/gov/nist/siplite/address;$(MAKE) -f makefile clean
	cd $(SRCROOT)/gov/nist/siplite/header;$(MAKE) -f makefile clean
	cd $(SRCROOT)/gov/nist/siplite/parser;$(MAKE) -f makefile clean

classfiles: 
	cd $(SRCROOT)/org/bouncycastle/crypto; $(MAKE) all
	cd $(SRCROOT)/org/bouncycastle/crypto/digests; $(MAKE) all
	cd $(SRCROOT)/gov/nist/core; $(MAKE) all
	cd $(SRCROOT)/gov/nist/siplite/address; $(MAKE) all
	cd $(SRCROOT)/gov/nist/siplite/header; $(MAKE) all
	cd $(SRCROOT)/gov/nist/siplite/stack; $(MAKE) all
	cd $(SRCROOT)/gov/nist/siplite; $(MAKE) all


all: classfiles preverifyall

cvstag:
	echo $(CVSTAG) >>  cvstag

tarfile:
	echo "Refer to this CVS Tag when reporting bugs" > cvstag
	cd ../; tar -cvzf nist-siplite-1.2.tar.gz \
	./nist-siplite-1.2		\
	--exclude *~		\
	--exclude .#*		\
	--exclude CVS 		\
	--exclude debug		\
	--exclude kxml2		\
	--exclude javaxxx	\
	--exclude classes	\
	--exclude preverified	\
	--exclude lcrypto-jdk14-112

ROOT=./
REV=alpha
DOCDIR= $(ROOT)/docs/api
WINDOWTITLE="SIP Tools For the People!"
HDR= '<b> NIST-SIPLITE SIP/SDP Parser and Stack REV API </b>'
HEADER=$(subst REV,$(REV),$(HDR))
DATE= $(shell date +%D)
TITL= 'NIST-SIPLITE SIP/SDP Parser and Stack CVSTAG'
CVST=REV-DATE
CVSTA=$(subst REV,$(REV),$(CVST))
CVSTAG=$(subst DATE,$(DATE),$(CVSTA))
TITLE=$(subst CVSTAG,$(CVSTAG),$(TITL))
DOCTITLE=$(subst DATE,$(DATE),$(TITLE))
BOTT='<font size="-1"> \
CVS ID = CVSTAG <br> \
<a href="http://w3.antd.nist.gov/"> A product of the NIST/ITL Advanced Networking Technologies Division. </a><br>  \
<a href="{@docRoot}/uncopyright.html"> See conditions of use. </a> <br> \
<a href="mailto:mranga@nist.gov"> Get the latest distribution. </a><br>  \
<a href="mailto:nist-sip-dev@antd.nist.gov">Submit a bug report or feature request.</a><br> \
</font>'

BOTTOM=$(subst CVSTAG,$(CVSTAG),$(BOTT))

javadoc:
	$(JAVADOC) -sourcepath $(ROOT)                  \
                -overview $(DOCDIR)/overview.html       \
                -d  $(DOCDIR)                           \
                -use                                    \
                -splitindex                             \
                -windowtitle $(WINDOWTITLE)             \
                -doctitle $(DOCTITLE)                   \
                -header $(HEADER)                       \
                -public                                 \
                -bottom $(BOTTOM)                       \
                gov.nist.siplite                           \
                gov.nist.siplite.net                        \
                gov.nist.siplite.sipheaders                 \
                gov.nist.siplite.sdpfields                  \
                gov.nist.siplite.stack                      \
                gov.nist.siplite.stack.security             \
                gov.nist.siplite.stack.transaction          \
                javax.siplite			          \
                gov.nist.javax.siplite           


export: tarfile
	cvs tag -d $(CVSTAG)
	cvs tag $(CVSTAG)

