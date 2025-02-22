#[cfg(feature = "headers")]
use safer_ffi::headers::Language;
use safer_ffi::prelude::*;
use std::ffi::c_char;

mod install_location;
mod keyboard_handler;

#[ffi_export]
fn free_c_string(ptr: *mut c_char) {
    if !ptr.is_null() {
        // Reconstruct the original repr_c::Box<c_string> and drop it
        unsafe { drop(Box::from_raw(ptr)); }
    }
}

#[cfg(feature = "headers")]
pub fn generate_headers() -> ::std::io::Result<()> {
    ::safer_ffi::headers::builder()
        .with_language(Language::C)
        .to_file("target/windows_helper.h")?
        .generate()
}
