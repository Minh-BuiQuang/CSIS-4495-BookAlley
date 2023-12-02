using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using BookAlleyWebApi.Models;
using BookAlleyWebApi.RestModels;

namespace BookAlleyWebApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class MessagesController : ControllerBase
    {
        private readonly BookAlleyContext _context;

        public MessagesController(BookAlleyContext context)
        {
            _context = context;
        }

        // POST: api/Messages
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<Message>> PostMessage([FromQuery] Guid sessionToken, MessageRequest messageRequest)
        {
            if (_context.Messages == null)
            {
                return Problem("Entity set 'BookAlleyContext.Messages'  is null.");
            }
            //Get user from session token
            var token = await _context.SessionTokens.Include(s => s.User).FirstOrDefaultAsync(s => s.Id == sessionToken);
            var user = token?.User;
            if (user == null)
            {
                return Problem("User not found, please login");
            }
            var conversation = await _context.Conversations.FindAsync(messageRequest.ConversationId);
            if(conversation == null)
            {
                return Problem("Conversation not found");
            }
            else
            if(messageRequest.Source.Equals("finder") && conversation.FinderId != user.Id)
            {
                return Problem("User is not the finder of this conversation");
            }
            else if(messageRequest.Source.Equals("poster") && conversation.PosterId != user.Id)
            {
                return Problem("User is not the poster of this conversation");
            }
            else if(!messageRequest.Source.Equals("finder") && !messageRequest.Source.Equals("poster"))
            {
                return Problem("Invalid request");
            }

            Message message = new()
            {
                Content = messageRequest.Content,
                Conversation = conversation,
                Source = ToMessageSource(messageRequest.Source),
                Timestamp = DateTimeOffset.Now
            };
            _context.Messages.Add(message);
            await _context.SaveChangesAsync();
            return Ok(new { message = "Message sent!" });
        }

        private Message.MessageSource ToMessageSource(string source)
        {
            return source switch
            {
                "system" => Message.MessageSource.system,
                "poster" => Message.MessageSource.poster,
                "finder" => Message.MessageSource.finder,
                _ => Message.MessageSource.system
            };
        }
    }
}
