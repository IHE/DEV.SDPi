:: Creates html output for all tests. This should make
:: all tests pass, but take care that the output generated
:: is correct!

call build_test_result full_requirement
call build_test_result ics_filtered
call build_test_result ics_no_filter
call build_test_result min_requirement
call build_test_result profiles
call build_test_result ref_ics_requirement
call build_test_result ref_variables
call build_test_result req-ownership
call build_test_result risk_requirement
call build_test_result test_level_input
call build_test_result test_offset_input
call build_test_result transaction-option
call build_test_result transactions
call build_test_result use_case
call build_test_result use_case_option
call build_test_result use_case_requirement
call build_test_result content-module
call build_test_result content-module-option
call build_test_result cross_ref_requirement
