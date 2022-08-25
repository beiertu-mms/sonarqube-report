# User binary directory, which should be in the $PATH env
BIN = $(HOME)/.local/bin

# Default distribution directory
DIST_DIR = ./build/dist

BUILDER = ./gradlew

uninstall:
	rm -v -f $(BIN)/cli-app-template* 2>/dev/null || echo "nothing to remove"

install: build
	chmod -v +x $(DIST_DIR)/cli-app-template.sh
	cp -v -f $(DIST_DIR)/cli-app-template.sh $(BIN)/cli-app-template
	cp -v -f $(DIST_DIR)/cli-app-template.jar $(BIN)/cli-app-template.jar

.PHONY: build
build: clean 
	$(BUILDER) packageDistribution --no-daemon

clean:
	$(BUILDER) clean --no-daemon
