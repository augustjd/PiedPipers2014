MAKE_DISPLAY = true

RATS    = 50
PIPERS  = 2

SIZE    = 500

PIPER_PROGRAM = dumb1 

SEED=$(shell date +%s)
run:
	echo $(SEED) > last.txt
	java piedpipers.sim.Piedpipers $(PIPER_PROGRAM) $(PIPERS) $(RATS) $(MAKE_DISPLAY) $(SEED) $(SIZE)

16k:
	java piedpipers.sim.Piedpipers $(PIPER_PROGRAM) $(PIPERS) $(RATS) $(MAKE_DISPLAY) $(shell cat 16k.txt) $(SIZE)

last:
	java piedpipers.sim.Piedpipers $(PIPER_PROGRAM) $(PIPERS) $(RATS) $(MAKE_DISPLAY) $(shell cat last.txt) $(SIZE)
last-headless:
	java piedpipers.sim.Piedpipers $(PIPER_PROGRAM) $(PIPERS) $(RATS) false $(shell cat last.txt) $(SIZE)

headless:
	java piedpipers.sim.Piedpipers $(PIPER_PROGRAM) $(PIPERS) $(RATS) false $(SEED) $(SIZE)
compile:
	javac ./piedpipers/sim/*.java
