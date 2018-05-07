# feup-sdis-voip
Development of a VoIP application

## Major WIP enhancements
- [ ] Establish connection when both users call eachother instead of both having to confirm it;
- [ ] Implement initial REGISTER rejection and provide challenge for credentials;
- [ ] One user may call other (which is registered) without itself registering;
- [ ] Place calls on hold by accepting other calls while one is ongoing;
- [x] Cancel registration by sending another REGISTER request;
- [ ] Tidy terminal mess (maybe log the packets instead of printing them);
- [ ] Tidy application interface (it's still kinda counter-intuitive), by running an input loop.
