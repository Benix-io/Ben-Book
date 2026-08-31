import os
import glob
import subprocess
import shutil
import sys

SDK_JAR = os.path.expanduser("~/android-sdk/platforms/android-34/android.jar")
BUILD_DIR = "build"
APK_UNSIGNED = f"{BUILD_DIR}/unsigned.apk"
APK_SIGNED = f"{BUILD_DIR}/BenBook-debug.apk"
KEYSTORE = "debug.keystore"

def run_cmd(cmd):
    result = subprocess.run(cmd, shell=True)
    if result.returncode != 0:
        print(f"\n[❌] Command failed: {cmd}")
        sys.exit(1)

def compile_and_build():
    print("[1/5] Cleaning and preparing build directories...")
    if os.path.exists(BUILD_DIR):
        shutil.rmtree(BUILD_DIR)
    os.makedirs(f"{BUILD_DIR}/compiled_res", exist_ok=True)
    os.makedirs(f"{BUILD_DIR}/gen", exist_ok=True)
    os.makedirs(f"{BUILD_DIR}/classes", exist_ok=True)

    print("[2/5] Compiling resources with AAPT2...")
    run_cmd(f"aapt2 compile --dir app/src/main/res -o {BUILD_DIR}/compiled_res/")
    
    flat_files = " ".join(glob.glob(f"{BUILD_DIR}/compiled_res/*.flat"))
    run_cmd(f"aapt2 link -I {SDK_JAR} --manifest app/src/main/AndroidManifest.xml "
            f"-o {APK_UNSIGNED} --java {BUILD_DIR}/gen {flat_files} --auto-add-overlay")

    print("[3/5] Compiling Java source code...")
    java_files = []
    for root, _, files in os.walk("app/src/main/java"):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))
    for root, _, files in os.walk(f"{BUILD_DIR}/gen"):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))

    run_cmd(f"javac -cp {SDK_JAR} -d {BUILD_DIR}/classes {' '.join(java_files)}")

    print("[4/5] Converting bytecode to DEX (d8)...")
    # Pass directory directly to d8 to prevent duplicate inner class collisions
    run_cmd(f"d8 --lib {SDK_JAR} --output {BUILD_DIR}/ $(find {BUILD_DIR}/classes -type f -name '*.class' | sort -u)")
    run_cmd(f"cd {BUILD_DIR} && zip -u unsigned.apk classes.dex")

    print("[5/5] Signing APK...")
    if not os.path.exists(KEYSTORE):
        run_cmd(f"keytool -genkey -v -keystore {KEYSTORE} -storepass android -alias androiddebugkey "
                f"-keypass android -keyalg RSA -keysize 2048 -validity 10000 "
                f"-dname 'CN=Ben,O=SystSync,C=RW'")
    
    run_cmd(f"apksigner sign --ks {KEYSTORE} --ks-pass pass:android --ks-key-alias androiddebugkey "
            f"--key-pass pass:android --out {APK_SIGNED} {APK_UNSIGNED}")
    print(f"\n[✅] Build Complete! APK ready at: {APK_SIGNED}")

def git_sync():
    msg = input("Commit message (default: 'feat: full benbook app'): ") or "feat: full benbook app"
    run_cmd("git add .")
    run_cmd(f"git commit -m '{msg}'")
    run_cmd("git push -u origin main")
    print("\n[🚀] Code successfully synchronized with GitHub!")

if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "sync":
        git_sync()
    else:
        compile_and_build()
