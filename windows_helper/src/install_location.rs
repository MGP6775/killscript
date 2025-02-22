use safer_ffi::ffi_export;
use safer_ffi::ඞ::repr_c;
use windows_registry::LOCAL_MACHINE;

#[ffi_export]
fn read_gta_location() -> repr_c::String {
    let location = _read_gta_location().unwrap();

    location.into()
}

fn _read_gta_location() -> windows_registry::Result<String> {
    let key = LOCAL_MACHINE.open("SOFTWARE\\WOW6432Node\\Rockstar Games\\Grand Theft Auto V")?;

    key.get_string("InstallFolder")
}

