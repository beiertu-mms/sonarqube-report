# User binary directory, which should be in the $PATH env
BIN = $(HOME)/.local/bin

# Default distribution directory
DIST_DIR = ./build/dist

BUILDER = ./gradlew

uninstall:
	rm -v -f $(BIN)/sonarqube-report* 2>/dev/null || echo "nothing to remove"

install: build
	chmod -v +x $(DIST_DIR)/sonarqube-report.sh
	cp -v -f $(DIST_DIR)/sonarqube-report.sh $(BIN)/sonarqube-report
	cp -v -f $(DIST_DIR)/sonarqube-report.jar $(BIN)/sonarqube-report.jar

.PHONY: build
build: clean 
	$(BUILDER) packageDistribution --no-daemon

clean:
	$(BUILDER) clean --no-daemon
