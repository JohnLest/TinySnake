ClientDirectoryBin = Client/bin
ServerDirectoryBin = Server/bin
ClientDirectorySrc = Client/src
ServerDirectorySrc = Server/src
DocumentsDirectory = Documents

cc-all: $(ClientDirectoryBin)/*.class $(ServerDirectoryBin)/*.class
cc-client: $(ClientDirectoryBin)/*.class
cc-server: $(ServerDirectoryBin)/*.class

$(ClientDirectoryBin)/%.class: $(ClientDirectorySrc)/%.java
	javac $(ClientDirectorySrc)/*.java -d $(ClientDirectoryBin) $<

$(ServerDirectoryBin)/%.class: $(ServerDirectorySrc)/%.java
	javac $(ServerDirectorySrc)/*.java -d $(ServerDirectoryBin) $<

run-server:
	java -classpath $(ServerDirectoryBin) App

run-client:
	java -classpath $(ClientDirectoryBin) App $(IP)

javadoc-client:
	@javadoc -d $(DocumentsDirectory)/javadoc-client -linksource $(ClientDirectorySrc)/*.java

javadoc-server:
	@javadoc -d $(DocumentsDirectory)/javadoc-server -linksource $(ServerDirectorySrc)/*.java
