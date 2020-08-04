import os
import shutil
import subprocess

print ("Putting hw3.tgz into \'./out\'...\n")

def get_javas(loc):
	pkg = []
	names = []
	for f in os.listdir(loc):
		if '.java' in f:
			pkg.append(os.path.join(loc, f))
			names.append(f)
	return pkg, names

# Gather source files and copy them to dest
dest = './out/hw3'
pkgs = ['./src/main/java/regalloc']

# Clean up old code
if os.path.isdir(dest):
	# Remove old .tgz if it exists
	tgz_path = os.path.join(dest, 'hw3.tgz')
	if os.path.isfile(tgz_path):
		os.remove(tgz_path)

	for filename in os.listdir(dest):
		file_path = os.path.join(dest, filename)
		try:
			if os.path.isfile(file_path) or os.path.islink(file_path):
				os.unlink(file_path)
			elif os.path.isdir(file_path):
				shutil.rmtree(file_path)
		except Exception as e:
			print('Failed to delete %s. Reason: %s' % (file_path, e))
	os.rmdir(dest)
os.mkdir(dest) # Make the folder for dest

for p in pkgs:
	files, names = get_javas(p)
	for i, f in enumerate(files):
		n = names[i]
		shutil.copyfile(f, os.path.join(dest, n))

# Clean up source files
# Remove this specific text from all files:
text_to_remove = ['package regalloc;']
for file in os.listdir(dest):
	filename = os.path.join(dest, file)
	with open(filename, "r") as f:
		lines = f.readlines()
	with open(filename, "w") as f:
		for line in lines:
			to_modify = line
			for t in text_to_remove:
				if t in line:
					to_modify = to_modify.replace(t, '')
			f.write(to_modify)

# Package source files
bashCommand = "tar -cvzf hw3.tgz ./hw3"
process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE, cwd='./out')
output, error = process.communicate()

print("Error: " + str(error))

bashCommand = "cp hw3.tgz ../tester/Phase3Tester"
process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE, cwd='./out')
output, error = process.communicate()

bashCommand = "./run SelfTestCases hw3.tgz"
process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE, cwd='./tester/Phase3Tester')
output, error = process.communicate()

print(output)

print("Error: " + str(error))
print("Done.")