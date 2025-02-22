use mki::Action;
use safer_ffi::prelude::*;
use std::sync::{Arc, Mutex};

pub static CALLBACK: once_cell::sync::Lazy<Arc<Mutex<Option<unsafe extern "C" fn(i32)>>>> =
    once_cell::sync::Lazy::new(|| Arc::new(Mutex::new(None)));

#[ffi_export]
fn register_keyboard_handler(
    cb: unsafe extern "C" fn(i32),
) {
    let mut callback = CALLBACK.lock().unwrap();
    *callback = Some(cb);
}

#[ffi_export]
fn register_keyboard_hook() {
    mki::bind_any_key(Action::handle_kb(|key| {
        call_webhook(key.into())
    }));
}

fn call_webhook(key: i32) {
    let callback = CALLBACK.lock().unwrap();
    if let Some(cb) = *callback {
        unsafe {
            cb(key);
        }
    }
}

