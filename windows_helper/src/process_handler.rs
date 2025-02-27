use runas::Command as RunAsCommand;
use safer_ffi::ffi_export;
use safer_ffi::ඞ::{c_slice, repr_c};
use std::os::windows::process::CommandExt;
use std::process::Command;

#[ffi_export]
fn spawn_detached_process(binary: repr_c::String) {
    const DETACHED_PROCESS: u32 = 0x00000008;
    const CREATE_NEW_PROCESS_GROUP: u32 = 0x00000200;

    Command::new(binary.to_string())
        .creation_flags(DETACHED_PROCESS | CREATE_NEW_PROCESS_GROUP)
        .spawn()
        .unwrap();
}

#[ffi_export]
fn spawn_elevated_process(binary: repr_c::String, args: c_slice::Ref<'_, repr_c::String>) -> i32 {
    // Collect owned strings to ensure lifetime safety
    let string_args: Vec<String> = args.as_slice()
        .iter()
        .map(|s| s.to_string())
        .collect();

    // Convert to &OsStr
    let os_args: Vec<&std::ffi::OsStr> = string_args
        .iter()
        .map(|s| std::ffi::OsStr::new(s))
        .collect();

    RunAsCommand::new(binary.to_string())
        .args(&os_args)
        .status()
        .unwrap()
        .code()
        .unwrap()
}
