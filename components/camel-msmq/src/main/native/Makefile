RM=del
MV=move
CP=copy
MKDIR=md
RMDIR=rd
CXX=cl
LINK=link

PSKDIR="C:\Program Files\Microsoft Platform SDK for Windows Server 2003 R2"

INCLUDES=/I$(JAVA_HOME)\include /I$(JAVA_HOME)\include\win32 /I$(PSKDIR)\include
CXXFLAGS=/EHsc $(INCLUDES)

SOURCES=msmq_native_support.cxx msmq_native_support_wrap.cxx

OBJS=$(SOURCES:.cxx=.obj)

LIBS=mqrt.lib

all: msmq_native_support.dll 

msmq_native_support.dll: $(SOURCES) $(OBJS)
			if exist $@ $(RM) $@
			if exist ..\..\..\src\main\resources\$@ $(RM) ..\..\..\src\main\resources\$@
			if exist ..\..\..\target\classes\$@ $(RM) $@ ..\..\..\target\classes\$@
			$(LINK) /DLL /libpath:$(PSDKDIR)\lib /OUT:$@ $(OBJS) $(LIBS)
			$(CP) $@ ..\..\..\src\main\resources
			if exist ..\..\..\target\classes $(CP) $@ ..\..\..\target\classes

cxx.obj:
	if exist $@ $(RM) $@
	$(CXX) -c $(CXXFLAGS) -Fo$@ $<          			
			
msmq_native_support_wrap.cxx: msmq_native_support.h
	swig -I. -java -package org.apache.camel.component.msmq.native_support -c++ -module msmq_native_support msmq_native_support.h
	$(MV) *.java ..\java\org\apache\camel\component\msmq\native_support
	
clean:
	$(RM) *.dll *.dps *.obj *.pdb *.ilk *.exp
	$(RM) ..\..\..\src\main\resources\*.dll
	if exist ..\..\..\target\classes $(RM) $@ ..\..\..\target\classes\$@
