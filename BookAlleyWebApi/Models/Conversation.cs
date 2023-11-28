namespace BookAlleyWebApi.Models
{
    public class Conversation
    {
        public long Id { get; set; }
        public long PosterId { get; set; }
        public long FinderId { get; set; }

        public virtual User Poster { get; set; }
        public virtual User Finder { get; set; }
        public virtual ICollection<Message> Messages { get; set; }
    }
}
